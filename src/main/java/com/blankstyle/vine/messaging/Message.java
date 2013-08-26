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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;

/**
 * A Vine message.
 *
 * @author Jordan Halterman
 */
public interface Message<T> extends Serializeable<JsonObject> {

  /**
   * Sets the unique message identifier.
   *
   * @param id
   *   The unique message identifier.
   * @return
   *   The called message object.
   */
  public Message<T> setIdentifier(String id);

  /**
   * Gets the unique message identifier.
   *
   * @return
   *   The unique message identifier.
   */
  public String getIdentifier();

  /**
   * Adds a tag to the message.
   *
   * @param tag
   *   The message tag.
   * @return
   *   The called message object.
   */
  public Message<T> addTag(String tag);

  /**
   * Gets a set of tags from the message.
   *
   * @return
   *   A set of message tags.
   */
  public String[] getTags();

  /**
   * Sets the message body.
   *
   * @param body
   *   The message body.
   * @return
   *   The called object.
   */
  public Message<T> setBody(T body);

  /**
   * Gets the message body.
   *
   * @return
   *   The message body.
   */
  public T getBody();

}