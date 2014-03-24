/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster.data.impl;

import net.kuujo.vertigo.cluster.data.DataException;
import net.kuujo.vertigo.cluster.data.AsyncList;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * An event bus list implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The list data type.
 */
public class EventBusList<T> implements AsyncList<T> {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
  private final String name;
  private final EventBus eventBus;

  public EventBusList(String name, EventBus eventBus) {
    this.name = name;
    this.eventBus = eventBus;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void add(T value) {
    add(value, null);
  }

  @Override
  public void add(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "add")
        .putString("type", "list")
        .putString("name", name)
        .putValue("value", value);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void remove(T value) {
    remove(value, null);
  }

  @Override
  public void remove(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "remove")
        .putString("type", "list")
        .putString("name", name)
        .putValue("value", value);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void remove(int index) {
    remove(index, null);
  }

  @Override
  public void remove(int index, final Handler<AsyncResult<T>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "remove")
        .putString("type", "list")
        .putString("name", name)
        .putValue("index", index);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<T>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<T>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<T>((T) result.result().body().getValue("result")).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void contains(Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "contains")
        .putString("type", "list")
        .putString("name", name)
        .putValue("value", value);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "size")
        .putString("type", "list")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Integer>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Integer>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<Integer>(result.result().body().getInteger("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "empty")
        .putString("type", "list")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void clear() {
    clear(null);
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "clear")
        .putString("type", "list")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void get(int index, final Handler<AsyncResult<T>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "get")
        .putString("type", "list")
        .putString("name", name)
        .putNumber("index", index);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<T>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<T>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<T>((T) result.result().body().getValue("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void set(int index, T value) {
    set(index, value, null);
  }

  @Override
  public void set(int index, T value, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
      .putString("action", "set")
      .putString("type", "list")
      .putString("name", name)
      .putNumber("index", index)
      .putValue("value", value);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

}
