/*
 *
 *  The contents of this file are subject to the Terracotta Public License Version
 *  2.0 (the "License"); You may not use this file except in compliance with the
 *  License. You may obtain a copy of the License at
 *
 *  http://terracotta.org/legal/terracotta-public-license.
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 *  the specific language governing rights and limitations under the License.
 *
 *  The Covered Software is Terracotta Core.
 *
 *  The Initial Developer of the Covered Software is
 *  Terracotta, Inc., a Software AG company
 *
 */
package com.tc.config;

import com.tc.net.TCSocketAddress;
import com.tc.net.groups.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupConfiguration {

  static final int SINGLE_SERVER_ELECTION_TIMEOUT = 0;
  static final int MULTI_SERVER_ELECTION_TIMEOUT = 5;

  private final Set<String> members = new HashSet<>();
  private final Set<Node> nodes = new HashSet<>();
  private final Node currentNode;

  GroupConfiguration(Map<String, ServerConfiguration> configMap, String serverName) {
    this.members.addAll(configMap.keySet());
    Node currentNode = null;
    for (Map.Entry<String, ServerConfiguration> member : configMap.entrySet()) {
      ServerConfiguration serverConfiguration = member.getValue();
      String bindAddress = serverConfiguration.getGroupPort().getBind();
      if (TCSocketAddress.WILDCARD_IP.equals(bindAddress)) {
        bindAddress = serverConfiguration.getHost();
      }
      Node node = new Node(bindAddress,
                           serverConfiguration.getTsaPort().getValue(),
                           serverConfiguration.getGroupPort().getValue());
      if (serverName.equals(member.getKey())) {
        currentNode = node;
      }
      nodes.add(node);
    }
    this.currentNode = currentNode;
  }

  public Set<Node> getNodes() {
    return nodes;
  }

  public Node getCurrentNode() {
    return currentNode;
  }

  public int getElectionTimeInSecs() {
    //TODO fix the election time
    // If there is only one server, always going to win so no reason to wait
    return (members.size() == 1) ? SINGLE_SERVER_ELECTION_TIMEOUT : MULTI_SERVER_ELECTION_TIMEOUT;
  }

  public String[] getMembers() {
    return this.members.toArray(new String[0]);
  }
}
