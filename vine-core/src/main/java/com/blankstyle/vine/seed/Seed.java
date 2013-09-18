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
package com.blankstyle.vine.seed;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.messaging.JsonMessage;

/**
 * A vine seed. Seeds instances are essentially individual tasks.
 *
 * @author Jordan Halterman
 */
public interface Seed {

  /**
   * Sets the seed vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called seed instance.
   */
  public Seed setVertx(Vertx vertx);

  /**
   * Sets the seed container instance.
   *
   * @param container
   *   A Vert.x container.
   * @return
   *   The called seed instance.
   */
  public Seed setContainer(Container container);

  /**
   * Sets the seed context.
   *
   * @param context
   *   A seed context.
   * @return
   *   The called seed instance.
   */
  public Seed setContext(WorkerContext context);

  /**
   * Sets a seed data handler.
   *
   * @param handler
   *   A json data handler.
   * @return 
   *   The called seed instance.
   */
  public Seed dataHandler(Handler<JsonObject> handler);

  /**
   * Emits data from the seed.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject data);

  /**
   * Emits multiple sets of data from the seed.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject... data);

  /**
   * Emits data to a specific seed.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param data
   *   The data to emit.
   */
  public void emitTo(String seedName, JsonObject data);

  /**
   * Emits multiple sets of data to a specific seed.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param data
   *   The data to emit.
   */
  public void emitTo(String seedName, JsonObject... data);

  /**
   * Acknowledges processing of a message.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage message);

  /**
   * Acknowledges processing of multiple messages.
   *
   * @param messages
   *   The messages to ack.
   */
  public void ack(JsonMessage... messages);

  /**
   * Fails processing of a message.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage message);

  /**
   * Fails processing of multiple messages.
   *
   * @param messages
   *   The messages to fail.
   */
  public void fail(JsonMessage... messages);

  /**
   * Starts the seed.
   */
  public void start();

}
