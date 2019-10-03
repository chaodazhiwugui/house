package czy.mooc.house.biz.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import czy.mooc.house.common.model.House;
import czy.mooc.house.common.page.PageParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    private static final String HOT_HOUSE_KEY = "hot_house";

    private static final Logger logger = LoggerFactory.getLogger(RecommendService.class);

    @Autowired
    private HouseService houseService;

    /**
     * 房产热度+1
     * 原理：在redis创建有序集合存放热点房产id
     */
    public void increase(Long id) {
        try {
            Jedis jedis = new Jedis("127.0.0.1");
            jedis.auth("123");
            //对redis有序集合中指定id的分数加上1
            jedis.zincrby(HOT_HOUSE_KEY, 1.0D, id + "");
            //0代表第一个元素,-1代表最后一个元素，因为要保留热度最高的10位，所以删除第1位到倒数第11位的元素，剩下10位热度最高的
            jedis.zremrangeByRank(HOT_HOUSE_KEY, 0, -11);
            jedis.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    //返回热点房产id
    public List<Long> getHot() {
        try {
            Jedis jedis = new Jedis("127.0.0.1");
            jedis.auth("123");
            //按从高到低排序取出所有元素  z + reverse + range
            Set<String> idSet = jedis.zrevrange(HOT_HOUSE_KEY, 0, -1);
            jedis.close();
            //Set<String>转换成List<Long>
            List<Long> ids = idSet.stream().map(Long::parseLong).collect(Collectors.toList());
            return ids;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Lists.newArrayList();
        }

    }

    /**
     * 获取热点房产
     */
    public List<House> getHotHouse(Integer size) {//size为需要显示的热点房产数
        House query = new House();
        //获取热点房产id集合
        List<Long> list = getHot();
        list = list.subList(0, Math.min(list.size(), size));
        if (list.isEmpty()) {
            return Lists.newArrayList();
        }
        query.setIds(list);
        final List<Long> order = list;
        //根据id集合查询房产集合
        List<House> houses = houseService.queryAndSetImg(query, PageParams.build(size, 1));
        //因为上面会打乱顺序，所以使用Ordering类让houses根据在order中的id顺序重新进行排序
        Ordering<House> houseSort = Ordering.natural().onResultOf(hs -> order.indexOf(hs.getId()));
        return houseSort.sortedCopy(houses);
    }

    /**
     * 获取最新房源
     */
    public List<House> getLastest() {
        House query = new House();
        query.setSort("create_time");//根据创建时间排序
        List<House> houses = houseService.queryAndSetImg(query, new PageParams(8, 1));
        return houses;
    }
}
