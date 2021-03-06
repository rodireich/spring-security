/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.config.annotation.web.configurers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.test.SpringTestRule;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 *
 * Tests for {@link HeaderWriterLogoutHandler} that passing {@link ClearSiteDataHeaderWriter}
 * implementation.
 *
 * @author Rafiullah Hamedy
 *
 */
@RunWith(SpringRunner.class)
@SecurityTestExecutionListeners
public class LogoutConfigurerClearSiteDataTests {

	private static final String CLEAR_SITE_DATA_HEADER = "Clear-Site-Data";

	private static final String[] SOURCE = {"cache", "cookies", "storage", "executionContexts"};

	private static final String HEADER_VALUE = "\"cache\", \"cookies\", \"storage\", \"executionContexts\"";

	@Rule
	public final SpringTestRule spring = new SpringTestRule();

	@Autowired
	MockMvc mvc;

	@Test
	@WithMockUser
	public void logoutWhenRequestTypeGetThenHeaderNotPresentt() throws Exception {
		this.spring.register(HttpLogoutConfig.class).autowire();

		this.mvc.perform(get("/logout").secure(true).with(csrf()))
			.andExpect(header().doesNotExist(CLEAR_SITE_DATA_HEADER));
	}

	@Test
	@WithMockUser
	public void logoutWhenRequestTypePostAndNotSecureThenHeaderNotPresent() throws Exception {
		this.spring.register(HttpLogoutConfig.class).autowire();

		this.mvc.perform(post("/logout").with(csrf()))
			.andExpect(header().doesNotExist(CLEAR_SITE_DATA_HEADER));
	}

	@Test
	@WithMockUser
	public void logoutWhenRequestTypePostAndSecureThenHeaderIsPresent() throws Exception {
		this.spring.register(HttpLogoutConfig.class).autowire();

		this.mvc.perform(post("/logout").secure(true).with(csrf()))
			.andExpect(header().stringValues(CLEAR_SITE_DATA_HEADER, HEADER_VALUE));
	}

	@EnableWebSecurity
	static class HttpLogoutConfig extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.logout()
					.addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(SOURCE)));
		}
	}
}