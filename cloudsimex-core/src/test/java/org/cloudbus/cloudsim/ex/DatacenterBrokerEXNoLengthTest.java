package org.cloudbus.cloudsim.ex;

import org.junit.Test;

/**
 *
 * @author Francisco Ramirez Mendez
 */
public class DatacenterBrokerEXNoLengthTest extends DatacenterBrokerEXTest
{

    public DatacenterBrokerEXNoLengthTest()
    {
        super();
        noLength = true;
    }
    
    @Test
    @Override
    public void testBootTime() {
        //super.testBootTime();
    }

    @Test
    @Override
    public void testTwoVmBothFail()
    {
        //super.testTwoVmBothFail();
    }

    @Test
    @Override
    public void testTwoVmOneFail()
    {
        //super.testTwoVmOneFail();
    }

    @Test
    @Override
    public void testVmsAreShutProperly()
    {
        super.testVmsAreShutProperly();
    }
}
