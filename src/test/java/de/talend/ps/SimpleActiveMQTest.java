package de.talend.ps;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.osgi.ActiveMQServiceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.io.File;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SimpleActiveMQTest {

    static final String KARAF_VERSION = "4.0.3";
    static final String ACTIVEMQ_VERSION = "5.11.3";
    static final MavenArtifactUrlReference KARAF_URL = maven()
            .groupId("org.apache.karaf")
            .artifactId("apache-karaf")
            .version(KARAF_VERSION)
            .type("zip");
    static final MavenArtifactUrlReference ACTIVEMQ_CORE_FEATURE = maven()
            .groupId("org.apache.activemq")
            .artifactId("activemq-karaf")
            .version(ACTIVEMQ_VERSION)
            .classifier("features-core")
            .type("xml");
    static final MavenArtifactUrlReference ACTIVEMQ_FEATURE = maven()
            .groupId("org.apache.activemq")
            .artifactId("activemq-karaf")
            .version(ACTIVEMQ_VERSION)
            .classifier("features")
            .type("xml");
    @Inject
    @Filter("(osgi.jndi.service.name=jms/activemq)")
    ConnectionFactory connectionFactory;
    DestinationSource destinationSource;

    @Configuration
    public Option[] config() {
        return options(
                karafDistributionConfiguration()
                        .frameworkUrl(KARAF_URL)
                        .karafVersion("4.0.3")
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(true),
                keepRuntimeFolder(),
                replaceConfigurationFile("tmp/activemq-connection-factory.xml",
                        new File("src/test/resources/activemq-connection-factory.xml")),
                features(ACTIVEMQ_CORE_FEATURE, "activemq"),
                features(ACTIVEMQ_FEATURE, "activemq-broker"),
                // publishes ConnectionFactory as OSGi service
                features(new File("src/test/resources/activemq-connection-factory-feature.xml").toURI().toString(),
                        "activemq-connection-factory")
        );
    }

    @Before
    public void setUp() throws InterruptedException, JMSException {
        Thread.sleep(2500);
        ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
        destinationSource = connection.getDestinationSource();
    }

    @Test
    public void letsTestIt() throws InterruptedException {
        assertEquals(0, destinationSource.getQueues().size());
        assertEquals(0, destinationSource.getTopics().size());
    }


}
