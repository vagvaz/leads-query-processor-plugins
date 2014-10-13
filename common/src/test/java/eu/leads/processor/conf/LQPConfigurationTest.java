package eu.leads.processor.conf;

import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;

public class LQPConfigurationTest extends TestCase {

    public void testGetInstance() throws Exception {

    }

    public void testGetConf() throws Exception {

    }

    public void testInitialize() throws Exception {
        LQPConfiguration.initialize();
        this.printConfig(LQPConfiguration.getConf());
    }

    private void printConfig(Configuration conf) {
        System.out.println(ConfigurationUtilities.getString(conf));
    }

    public void testInitialize1() throws Exception {
        LQPConfiguration.initialize(false);
        this.printConfig(LQPConfiguration.getConf());

    }

    public void testInitialize2() throws Exception {
        LQPConfiguration.initialize("conf", false);
        this.printConfig(LQPConfiguration.getConf());
    }

    public void testGetConfiguration() throws Exception {

    }

    public void testGetMicroClusterName() throws Exception {

    }

    public void testGetNodeName() throws Exception {

    }

    public void testGetHostname() throws Exception {

    }
}
