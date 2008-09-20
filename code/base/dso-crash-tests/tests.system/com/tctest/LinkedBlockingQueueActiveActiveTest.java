/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest;

import com.tc.test.MultipleServersCrashMode;
import com.tc.test.MultipleServersPersistenceMode;
import com.tc.test.MultipleServersSharedDataMode;
import com.tc.test.activeactive.ActiveActiveTestSetupManager;

public class LinkedBlockingQueueActiveActiveTest extends ActiveActiveTransparentTestBase {

  private static final int NODE_COUNT   = 1;
  private final int        electionTime = 5;

  public LinkedBlockingQueueActiveActiveTest() {
    disableAllUntil("2019-10-01");
  }

  public void doSetUp(TransparentTestIface t) throws Exception {
    t.getTransparentAppConfig().setClientCount(NODE_COUNT);
    t.initializeTestRunner();
  }

  protected Class getApplicationClass() {
    return LinkedBlockingQueueCrashTestApp.class;
  }

  public void setupActiveActiveTest(ActiveActiveTestSetupManager setupManager) {
    setupManager.setServerCount(4);
    setupManager.setServerCrashMode(MultipleServersCrashMode.NO_CRASH);
    setupManager.setServerShareDataMode(MultipleServersSharedDataMode.NETWORK);
    setupManager.setServerPersistenceMode(MultipleServersPersistenceMode.PERMANENT_STORE);
    setupManager.addActiveServerGroup(2, MultipleServersSharedDataMode.NETWORK, electionTime);
    setupManager.addActiveServerGroup(2, MultipleServersSharedDataMode.NETWORK, electionTime);
  }
}
