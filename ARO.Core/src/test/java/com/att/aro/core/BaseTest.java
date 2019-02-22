package com.att.aro.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AROConfig.class)
public class BaseTest {
	@Autowired
	protected ApplicationContext context;
	
	//this blank method need to be here to avoid error
	@Test
	public void testSuppression(){
		
	}
}
