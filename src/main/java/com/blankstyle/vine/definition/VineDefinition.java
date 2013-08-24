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
package com.blankstyle.vine.definition;

import com.blankstyle.vine.Definition;

/**
 * A Vine context.
 *
 * @author Jordan Halterman
 */
public interface VineDefinition extends Definition<VineDefinition> {

  /**
   * Gets the vine address.
   *
   * @return
   *   The vine address.
   */
  public String getAddress();

  /**
   * Sets the vine address.
   *
   * @param address
   *   The vine address.
   * @return
   *   The called vine definition.
   */
  public VineDefinition setAddress(String address);

  /**
   * Adds a new seed to the vine.
   *
   * @param context
   *   The seed context.
   * @return
   *   The seed context.
   */
  public SeedDefinition feed(SeedDefinition context);

  /**
   * Adds a new seed to the vine.
   *
   * @param name
   *   The seed name.
   * @return
   *   The seed context.
   */
  public SeedDefinition feed(String name);

  /**
   * Adds a new seed to the vine.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @return
   *   The seed context.
   */
  public SeedDefinition feed(String name, String main);

  /**
   * Adds a new seed to the vine.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @param workers
   *   The number of seed workers.
   * @return
   *   The seed context.
   */
  public SeedDefinition feed(String name, String main, int workers);

}
