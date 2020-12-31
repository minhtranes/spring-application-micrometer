package vn.minhtran.sm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;

@Configuration
@EnableScheduling
public class AppConfiguration {

    @Bean
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }

    @Bean
    public MeterFilter hostTagMeterFilter() throws UnknownHostException {
        return MeterFilter.commonTags(
            Arrays.asList(
                Tag.of("host", InetAddress.getLocalHost().getHostName())));

    }
}
