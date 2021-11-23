package redis.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import javax.crypto.KeyGeneratorSpi;
import java.lang.reflect.Method;
import java.security.Provider;

public class CustomKeyGenerator implements KeyGenerator {

    public Object generate(Object target, Method method, Object... params){
        return target.getClass().getSimpleName()+"-"+method.getName()+"-"+ StringUtils.arrayToDelimitedString(params, "-");
    }
}
