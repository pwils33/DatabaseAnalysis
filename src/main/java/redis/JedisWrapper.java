package redis;

import redis.clients.jedis.Jedis;

public class JedisWrapper {

    private Jedis jedis;

    public JedisWrapper() {
        this.jedis = new Jedis();
    }

    public Jedis getJedis() {
        return jedis;
    }
}
