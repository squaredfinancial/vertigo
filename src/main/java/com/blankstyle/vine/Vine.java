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
package com.blankstyle.vine;

import java.util.Collection;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.definition.JsonVineDefinition;
import com.blankstyle.vine.definition.VineDefinition;

/**
 * An abstract vine.
 *
 * @author Jordan Halterman
 */
public abstract class Vine implements Deployer<Collection<Seed>, String> {

  /**
   * Creates a new vine context.
   *
   * @return
   *   A new vine context.
   */
  public static VineDefinition createContext() {
    return new JsonVineDefinition();
  }

  /**
   * Creates a new vine context.
   *
   * @param address
   *   The context address.
   * @return
   *   A new vine context.
   */
  public static VineDefinition createContext(String address) {
    return new JsonVineDefinition().setAddress(address);
  }

  protected JsonObject context;

  public Vine(JsonObject context) {
    this.context = context;
  }

}
