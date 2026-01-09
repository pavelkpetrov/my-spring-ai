package org.springframework.ai.mcp.sample.client;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;

/**
 * With stdio transport, the MCP server is automatically started by the client.
 * It is necessary to build the server jar first:
 *
 * <pre>
 * ./mvnw clean install -DskipTests
 * </pre>
 */
public class ClientStdio {

	public static void main(String[] args) {

		System.out.println(new File(".").getAbsolutePath());

		var stdioParams = ServerParameters.builder("java")
				.args("-Dspring.ai.mcp.server.stdio=true", "-Dspring.main.web-application-type=none",
						"-Dlogging.pattern.console=", "-jar",
						"./mcp-stdio-server/target/mcp-stdio-server-0.0.1-SNAPSHOT.jar")
				.build();

		var transport = new StdioClientTransport(stdioParams, new ObjectMapper());

		new SampleClient(transport).run();
	}

}
