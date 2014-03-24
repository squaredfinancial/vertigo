/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.java;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import static net.kuujo.vertigo.util.Config.parseCluster;
import static net.kuujo.vertigo.util.Config.parseNetwork;
import static net.kuujo.vertigo.util.Config.parseAddress;
import static net.kuujo.vertigo.util.Config.populateConfig;

/**
 * Base class for Java vertigo component verticle implementations.
 * 
 * @author Jordan Halterman
 */
abstract class ComponentVerticle<T extends Component<T>> extends Verticle {
  protected Vertigo vertigo;
  protected VertigoCluster cluster;
  protected String address;
  protected InstanceContext context;
  protected JsonObject config;
  protected Logger logger;

  /**
   * Creates a component instance for the verticle.
   * 
   * @param network The network address.
   * @param address The component instance address.
   * @return The new component instance.
   */
  protected abstract T createComponent(String network, String address);

  /**
   * Because of the method by which Vertigo coordinates starting of component instances,
   * we do not use the future-based start method here. Instead, we simply allow the
   * Vertigo coordinator to do its job and then provide a separate start method for when
   * the component is actually started (which differs from when the verticle is started).
   */
  @Override
  public void start() {
    logger = container.logger();
    cluster = parseCluster(container.config(), vertx, container);
    String network = parseNetwork(container.config());
    address = parseAddress(container.config());
    populateConfig(container.config());
    config = container.config();
    final T component = createComponent(network, address);
    vertigo = new Vertigo(this);

    component.start(new Handler<AsyncResult<T>>() {
      @Override
      public void handle(AsyncResult<T> result) {
        if (result.succeeded()) {
          context = component.context();
          start(component);
        }
      }
    });
  }

  /**
   * Called when the component has been started.
   * 
   * This method differs from the normal {@link #start()} method in that this method will
   * be called *after* all other start methods. This is because this method is actually
   * called once the component has connected to all of its interested trading partners.
   * 
   * @param component The component that was started.
   */
  protected void start(T component) {
  }

}
