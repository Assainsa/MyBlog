package com.lintao.blog;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
class BlogApiApplicationTests {

	@Test
	void contextLoads() throws ParseException {
		long currentTimeMillis = 20210618235508l;
		Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(String.valueOf(currentTimeMillis));
		String s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		/*		Date format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").parse(currentTime);*/
		System.out.println(currentTimeMillis);
		System.out.println(s);
	}

}
