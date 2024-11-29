package org.chenile.samples.order;

import org.chenile.stm.STMSecurityStrategy;

public class MockSS implements STMSecurityStrategy {
    public static String userName = "";
    @Override
    public boolean isAllowed(String... acls) {
        if (acls == null || acls.length == 0) return true;
        if (userName.equals("minimal_user") && acls[0].equals("payment-user")) return true;
        if (userName.equals("bank") && acls[0].equals("payment-checker")) return true;
        if( userName.equals("finance")) return true;
        return false;
    }
}