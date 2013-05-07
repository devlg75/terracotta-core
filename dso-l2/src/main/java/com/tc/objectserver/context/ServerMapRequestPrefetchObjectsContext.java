/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.objectserver.context;

import com.tc.async.api.Sink;
import com.tc.io.TCByteBufferOutputStream;
import com.tc.net.ClientID;
import com.tc.object.ObjectID;
import com.tc.object.ServerMapGetValueResponse;
import com.tc.object.dna.api.DNA.DNAType;
import com.tc.object.dna.impl.ObjectStringSerializerImpl;
import com.tc.objectserver.api.ObjectManager;
import com.tc.objectserver.api.ObjectManagerLookupResults;
import com.tc.objectserver.core.api.ManagedObject;
import com.tc.objectserver.l1.api.ClientStateManager;
import com.tc.util.ObjectIDSet;
import com.tc.util.TCCollections;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Map;

public class ServerMapRequestPrefetchObjectsContext implements ObjectManagerResultsContext {

    private final ClientID  clientid;
    private final ObjectID  mapid;
    private Map<ObjectID, ManagedObject> lookedUp;
    private final Collection<ServerMapGetValueResponse> answers = new ArrayList<ServerMapGetValueResponse>();
    private final ObjectStringSerializerImpl serializer = new ObjectStringSerializerImpl();
    private final Sink destination;
    
    
    public ServerMapRequestPrefetchObjectsContext(final ClientID clientID, final ObjectID oid, final Sink destination) {
      this.clientid = clientID;
      this.mapid = oid;
      this.destination = destination;
    }
    
    public Collection<ServerMapGetValueResponse> getAnswers() {
      return answers;
    }

    public ObjectStringSerializerImpl getSerializer() {
      return serializer;
    }
    
    public boolean shouldPrefetch() {
      for ( ServerMapGetValueResponse response : answers ) {
        if ( !response.getObjectIDs().isEmpty() ) {
          return true;
        }
      }
      return false;
    }

    public void addResponse(ServerMapGetValueResponse resp) {
      answers.add(resp);
    }
    
    public ClientID getClientID() {
      return clientid;
    }
    
    
    public ObjectID getMapID() {
      return mapid;
    }    

    @Override
    public ObjectIDSet getLookupIDs() {
      ObjectIDSet set = new ObjectIDSet();
      for ( ServerMapGetValueResponse resp : answers ) {
        set.addAll(resp.getObjectIDs());
      }
      return set;
    }
    
    public void releaseAll(ObjectManager mgr) {
  // could be null if there was nothing to prefetch.
      if ( lookedUp != null ) {
        mgr.releaseAll(lookedUp.values());
      }
    }
    
    public int prefetchObjects(ClientStateManager state) {
      int count = 0;
      for ( ServerMapGetValueResponse resp : answers ) {
        for ( ObjectID oid : new ArrayList<ObjectID>(resp.getObjectIDs()) ) {
          ManagedObject mo = lookedUp.get(oid);
          if ( mo != null && !state.hasReference(clientid, oid) ) {
            state.addReference(clientid, oid);
            TCByteBufferOutputStream out = new TCByteBufferOutputStream();
            mo.toDNA(out, serializer, DNAType.L1_FAULT);
            resp.replace(oid, out);
            count++;
          }
        }
      }
      return count;
    }

    @Override
    public ObjectIDSet getNewObjectIDs() {
      return TCCollections.EMPTY_OBJECT_ID_SET;
    }

    @Override
    public void setResults(ObjectManagerLookupResults results) {
      lookedUp = results.getObjects();
      this.destination.add(this);
    }
    
    @Override
    public String toString() {
      return super.toString() + " [ value requests : " + this.answers + "]";
    }

    
}