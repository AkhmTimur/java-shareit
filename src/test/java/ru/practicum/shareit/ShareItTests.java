package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItTests.class)
class ShareItTests {

	@Test
	void contextLoads(ApplicationContext context) {
		assertNotNull(context);
	}

}
