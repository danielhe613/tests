package demo;

import java.net.URI;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RibbonClientApplication.class)
@IntegrationTest("debug=true")
@WebAppConfiguration
public class RibbonClientApplicationTests {

	@Autowired
	@LoadBalanced
	private OAuth2RestTemplate oauth2RestTemplate;

	private MockHttpServletRequest request = new MockHttpServletRequest();

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@After
	public void clean() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void oauth2RestTemplateHasLoadBalancer() throws Exception {
		// Just to prove that the interceptor is present...
		ClientHttpRequest request = oauth2RestTemplate.getRequestFactory()
				.createRequest(new URI("http://nosuchservice"), HttpMethod.GET);
		expected.expectMessage("No instances available for nosuchservice");
		request.execute();
	}

	@Test
	public void useRestTemplate() throws Exception {
		// There's nowhere to get an access token so it should fail, but in a sensible way
		this.expected.expect(UserRedirectRequiredException.class);
		RequestContextHolder
				.setRequestAttributes(new ServletRequestAttributes(this.request));
		this.oauth2RestTemplate.getForEntity("http://foo/bar", String.class);
	}

}
