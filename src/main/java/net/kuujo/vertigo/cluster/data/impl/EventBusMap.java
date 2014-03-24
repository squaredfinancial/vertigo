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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kuujo.vertigo.cluster.data.DataException;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.MapEvent.Type;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxException;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * An event bus map implementation.
 *
 * @author Jordan Halterman
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public class EventBusMap<K, V> implements WatchableAsyncMap<K, V> {
  private final String CLUSTER_ADDRESS = "__CLUSTER__";
  private final String name;
  private final EventBus eventBus;
  private final Map<String, Map<Handler<MapEvent<K, V>>, HandlerWrapper>> watchHandlers = new HashMap<>();

  private static class HandlerWrapper {
    private final String address;
    private final Handler<Message<JsonObject>> messageHandler;

    private HandlerWrapper(String address, Handler<Message<JsonObject>> messageHandler) {
      this.address = address;
      this.messageHandler = messageHandler;
    }
  }

  public EventBusMap(String name, EventBus eventBus) {
    this.name = name;
    this.eventBus = eventBus;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void put(K key, V value) {
    put(key, value, null);
  }

  @Override
  public void put(K key, V value, final Handler<AsyncResult<V>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "put")
        .putString("type", "map")
        .putString("name", name)
        .putValue("key", key)
        .putValue("value", value);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<V>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<V>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<V>((V) result.result().body().getValue("result")).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void get(K key, final Handler<AsyncResult<V>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "get")
        .putString("type", "map")
        .putString("name", name)
        .putValue("key", key);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<V>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<V>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<V>((V) result.result().body().getValue("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void remove(K key) {
    remove(key, null);
  }

  @Override
  public void remove(K key, final Handler<AsyncResult<V>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "remove")
        .putString("type", "map")
        .putString("name", name)
        .putValue("key", key);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<V>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<V>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<V>((V) result.result().body().getValue("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void containsKey(K key, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "contains")
        .putString("type", "map")
        .putString("name", name)
        .putValue("key", key);
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
  public void keySet(final Handler<AsyncResult<Set<K>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "keys")
        .putString("type", "map")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Set<K>>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Set<K>>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          JsonArray jsonKeys = result.result().body().getArray("result");
          if (jsonKeys != null) {
            Set<K> keys = new HashSet<>();
            for (Object key : jsonKeys) {
              keys.add((K) key);
            }
            new DefaultFutureResult<Set<K>>(keys).setHandler(resultHandler);
          }
          else {
            new DefaultFutureResult<Set<K>>(new DataException("Invalid response.")).setHandler(resultHandler);
          }
        }
      }
    });
  }

  @Override
  public void values(final Handler<AsyncResult<Collection<V>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "values")
        .putString("type", "map")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<V>>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<V>>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          JsonArray jsonValues = result.result().body().getArray("result");
          if (jsonValues != null) {
            List<V> values = new ArrayList<>();
            for (Object value : jsonValues) {
              values.add((V) value);
            }
            new DefaultFutureResult<Collection<V>>(values).setHandler(resultHandler);
          }
          else {
            new DefaultFutureResult<Collection<V>>(new DataException("Invalid response.")).setHandler(resultHandler);
          }
        }
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "size")
        .putString("type", "map")
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
        .putString("type", "map")
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
        .putString("type", "map")
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
  public void watch(String key, Handler<MapEvent<K, V>> handler) {
    watch(key, null, handler, null);
  }

  @Override
  public void watch(String key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    watch(key, null, handler, doneHandler);
  }

  @Override
  public void watch(String key, Type event, Handler<MapEvent<K, V>> handler) {
    watch(key, event, handler, null);
  }

  @Override
  public void watch(final String key, final Type event, final Handler<MapEvent<K, V>> handler, final Handler<AsyncResult<Void>> doneHandler) {
    final String id = UUID.randomUUID().toString();
    final Handler<Message<JsonObject>> watchHandler = new Handler<Message<JsonObject>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(Message<JsonObject> message) {
        handler.handle(new MapEvent<K, V>(MapEvent.Type.parse(message.body().getString("event")), (K) message.body().getValue("key"), (V) message.body().getValue("value")));
      }
    };

    final HandlerWrapper wrapper = new HandlerWrapper(id, watchHandler);

    if (!watchHandlers.containsKey(key)) {
      watchHandlers.put(key, new HashMap<Handler<MapEvent<K, V>>, HandlerWrapper>());
    }

    final Map<Handler<MapEvent<K, V>>, HandlerWrapper> handlers = watchHandlers.get(key);

    eventBus.registerHandler(id, watchHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          handlers.put(handler, wrapper);
          JsonObject message = new JsonObject()
              .putString("action", "watch")
              .putString("name", name)
              .putValue("key", key)
              .putString("event", event != null ? event.toString() : null)
              .putString("address", id);
          eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
            @Override
            public void handle(AsyncResult<Message<JsonObject>> result) {
              if (result.failed()) {
                eventBus.unregisterHandler(id, watchHandler);
                handlers.remove(handler);
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              }
              else {
                JsonObject body = result.result().body();
                if (body.getString("status").equals("error")) {
                  eventBus.unregisterHandler(id, watchHandler);
                  handlers.remove(handler);
                  new DefaultFutureResult<Void>(new VertxException(body.getString("message"))).setHandler(doneHandler);
                }
                else {
                  new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
                }
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void unwatch(String key, Handler<MapEvent<K, V>> handler) {
    unwatch(key, null, handler, null);
  }

  @Override
  public void unwatch(String key, Type event, Handler<MapEvent<K, V>> handler) {
    unwatch(key, event, handler, null);
  }

  @Override
  public void unwatch(String key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    unwatch(key, null, handler, doneHandler);
  }

  @Override
  public void unwatch(final String key, final Type event, final Handler<MapEvent<K, V>> handler, final Handler<AsyncResult<Void>> doneHandler) {
    if (!watchHandlers.containsKey(key)) {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      return;
    }

    final Map<Handler<MapEvent<K, V>>, HandlerWrapper> handlers = watchHandlers.get(key);
    if (!handlers.containsKey(handler)) {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      return;
    }

    JsonObject message = new JsonObject()
        .putString("action", "unwatch")
        .putValue("key", key)
        .putString("event", event != null ? event.toString() : null)
        .putString("address", handlers.get(handler).address);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          HandlerWrapper wrapper = handlers.remove(handler);
          eventBus.unregisterHandler(wrapper.address, wrapper.messageHandler);
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          HandlerWrapper wrapper = handlers.remove(handler);
          eventBus.unregisterHandler(wrapper.address, wrapper.messageHandler, doneHandler);
        }
      }
    });
  }

}
