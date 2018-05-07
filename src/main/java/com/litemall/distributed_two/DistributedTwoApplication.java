package com.litemall.distributed_two;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.litemall.distributed_two","org.linlinjava.litemall.db"})
@MapperScan("org.linlinjava.litemall.db.dao")
public class DistributedTwoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedTwoApplication.class, args);
	}
}
