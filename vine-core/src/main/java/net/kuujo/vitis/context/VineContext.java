/*
* Copyright 2013 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.kuujo.vitis.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.kuujo.vitis.definition.VineDefinition;

import org.vertx.java.core.json.JsonObject;

/**
 * A remote vine context.
 *
 * @author Jordan Halterman
 */
public class VineContext implements Context {

  private JsonObject context = new JsonObject();

  public VineContext() {
  }

  public VineContext(JsonObject json) {
    context = json;
  }

  /**
   * Returns the vine address.
   */
  public String getAddress() {
    return context.getString("address");
  }

  /**
   * Returns a list of feeder connection contexts.
   */
  public Collection<ShootContext> getConnectionContexts() {
    Set<ShootContext> contexts = new HashSet<ShootContext>();
    JsonObject connections = context.getObject("connections");
    Iterator<String> iter = connections.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new ShootContext(connections.getObject(iter.next())));
    }
    return contexts;
  }

  /**
   * Returns a specific feeder connection context.
   *
   * @param name
   *   The connection (seed) name.
   */
  public ShootContext getConnectionContext(String name) {
    JsonObject connection = context.getObject("connections", new JsonObject()).getObject(name);
    if (connection != null) {
      return new ShootContext(connection);
    }
    return new ShootContext();
  }

  /**
   * Returns a list of vine seed contexts.
   */
  public Collection<NodeContext> getSeedContexts() {
    JsonObject seeds = context.getObject("seeds");
    ArrayList<NodeContext> contexts = new ArrayList<NodeContext>();
    Iterator<String> iter = seeds.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new NodeContext(seeds.getObject(iter.next()), this));
    }
    return contexts;
  }

  /**
   * Returns a specific seed context.
   *
   * @param name
   *   The seed name.
   */
  public NodeContext getSeedContext(String name) {
    JsonObject seeds = context.getObject("seeds");
    if (seeds == null) {
      return null;
    }
    JsonObject seedContext = seeds.getObject(name);
    if (seedContext == null) {
      return null;
    }
    return new NodeContext(seedContext);
  }

  /**
   * Returns the vine definition.
   *
   * @return
   *   The vine definition.
   */
  public VineDefinition getDefinition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new VineDefinition(definition);
    }
    return new VineDefinition();
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
