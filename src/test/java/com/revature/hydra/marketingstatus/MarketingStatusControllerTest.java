package com.revature.hydra.marketingstatus;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.revature.beans.MarketingStatus;
import com.revature.hydra.marketingstatus.application.MarketingStatusRepositoryServiceApplication;
import com.revature.hydra.marketingstatus.data.MarketingStatusRepository;

/**
 * JUnit class to test the Controller endpoints using Spring MVC Testing Framework
 * @author Omowumi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MarketingStatusRepositoryServiceApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketingStatusControllerTest {
	private static final Logger log = Logger.getLogger(MarketingStatusControllerTest.class);
	
	@Autowired
    private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MarketingStatusRepository marketingStatusRepository;
	
	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {
		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
					.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
					.findAny()
					.orElse(null);
		
		Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}
	
	private final String mediaTypeJson = MediaType.APPLICATION_JSON_UTF8_VALUE;
	
	private HttpMessageConverter mappingJackson2HttpMessageConverter;
	
	private MockMvc mockMvc;
	
	private MarketingStatus testMs;
	private MarketingStatus addMs;
	
	/**
	 * Create a test marketing status in table to test on
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		log.info("setUp: ");
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		this.testMs = new MarketingStatus(0, "TEST STATUS");
		this.testMs = this.marketingStatusRepository.save(this.testMs);
	}

	/**
	 * Remove test marketing status so that it doesn't cause problems with repeated runs 
	 * of the test and isn't left in database for production
	 */
	@After
	public void tearDown() {
		log.info("tearDown: ");
		int testId = this.testMs.getMarketingStatusId();
		if (marketingStatusRepository.findOne(testId) != null) {
			marketingStatusRepository.delete(testId);
		}
	}

	/**
	 * Test receiving on marketing status by id
	 * @throws Exception
	 */
	@Test
	public void test1OneMsById() throws Exception {
		log.info("test1OneMsById: ");
		this.mockMvc.perform(get("/one/marketingstatus/byid/" + this.testMs.getMarketingStatusId()))
					.andExpect(status().isOk())
					.andExpect(content().contentType(mediaTypeJson))
					.andExpect(jsonPath("$.marketingStatusId", is(this.testMs.getMarketingStatusId())))
					.andExpect(jsonPath("$.marketingStatusName", is(this.testMs.getMarketingStatusName())));
	}
	
	/**
	 * Test receiving all marketing status'
	 * @throws Exception
	 */
	@Test
	public void test2AllMs() throws Exception {
		log.info("test2AllMs: ");
		this.mockMvc.perform(get("/all/marketingstatus"))
					.andExpect(status().isOk())
					.andExpect(content().contentType(mediaTypeJson));
	}
	
	/**
	 * Test receiving all marketing status's in a map that can be accessed by id
	 * @throws Exception
	 */
	@Test
	public void test3AllMsMapped() throws Exception {
		log.info("test3AllMsMapped: ");
		this.mockMvc.perform(get("/all/marketingstatus/mapped"))
					.andExpect(status().isOk())
					.andExpect(content().contentType(mediaTypeJson))
					.andExpect(jsonPath("$.2.marketingStatusId", is(2)));
	}
	
	/**
	 * Test receiving a marketing status by name
	 * @throws Exception
	 */
	@Test
	public void test4OneMsByName() throws Exception {
		log.info("test4OneMsByName: ");
		this.mockMvc.perform(get("/one/marketingstatus/" + this.testMs.getMarketingStatusName()))
					.andExpect(status().isOk())
					.andExpect(content().contentType(mediaTypeJson))
					.andExpect(jsonPath("$.marketingStatusId", is(this.testMs.getMarketingStatusId())));
	}
	
	/**
	 * Test adding a marketing status to the database
	 * @throws Exception
	 */
	@Test
	public void test5AddMs() throws Exception {
		log.info("test5AddMs: ");
		this.addMs = new MarketingStatus();
		this.addMs.setMarketingStatusName("ADDTESTMS");
		this.mockMvc.perform(post("/add/marketingstatus")
					.content(this.json(addMs))
					.contentType(mediaTypeJson))
					.andExpect(status().isCreated());
	}
	
	/**
	 * Testing updating a marketing status' name
	 * @throws Exception
	 */
	@Test
	public void test6UpdateMs() throws Exception {
		log.info("test6UpdateMs: ");
		this.testMs = this.marketingStatusRepository.findOne(this.testMs.getMarketingStatusId());
		this.testMs.setMarketingStatusName("UPDATETESTMS");
		this.mockMvc.perform(put("/update/marketingstatus")
					.content(this.json(this.testMs))
					.contentType(this.mediaTypeJson))
					.andExpect(status().isOk());
	}
	
	/**
	 * Test deleting a marketing status
	 * @throws Exception
	 */
	@Test
	public void test7DeleteMs() throws Exception {
		log.info("test7DeleteMs: ");
		this.mockMvc.perform(delete("/delete/marketingstatus/" + this.testMs.getMarketingStatusId()))
					.andExpect(status().isOk());
	}
	
	/**
	 * Used to convert a java object into a json
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	protected String json(Object obj) throws IOException {
		log.info("json: ");
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(obj, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

}
