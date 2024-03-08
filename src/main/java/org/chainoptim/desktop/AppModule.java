package org.chainoptim.desktop;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.abstraction.GuiceControllerFactory;
import org.chainoptim.desktop.core.abstraction.JavaFXThreadRunner;
import org.chainoptim.desktop.core.abstraction.ThreadRunner;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.repository.UserRepositoryImpl;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.service.AuthenticationServiceImpl;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.features.factory.service.FactoryServiceImpl;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.features.product.service.ProductServiceImpl;
import org.chainoptim.desktop.features.product.service.ProductWriteService;
import org.chainoptim.desktop.features.product.service.ProductWriteServiceImpl;
import org.chainoptim.desktop.features.supplier.service.SupplierService;
import org.chainoptim.desktop.features.supplier.service.SupplierServiceImpl;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.features.warehouse.service.WarehouseServiceImpl;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderServiceImpl;

import java.net.http.HttpClient;

/*
 * Module configuring Dependency Injections
 *
 */
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        // Singletons
        bind(NavigationServiceImpl.class).asEagerSingleton();
        bind(FallbackManager.class).in(Singleton.class);
        bind(CurrentSelectionService.class).in(Singleton.class);
        bind(SearchParams.class).in(Singleton.class);

        bind(HttpClient.class).toInstance(HttpClient.newHttpClient());

        // Bind interfaces to implementations
        // Services
        bind(ControllerFactory.class).to(GuiceControllerFactory.class);
        bind(ThreadRunner.class).to(JavaFXThreadRunner.class);
        bind(NavigationService.class).to(NavigationServiceImpl.class);
        bind(FXMLLoaderService.class).to(FXMLLoaderServiceImpl.class);

        // Repositories
        bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
        bind(UserRepository.class).to(UserRepositoryImpl.class);
        bind(ProductService.class).to(ProductServiceImpl.class);
        bind(ProductWriteService.class).to(ProductWriteServiceImpl.class);
        bind(FactoryService.class).to(FactoryServiceImpl.class);
        bind(WarehouseService.class).to(WarehouseServiceImpl.class);
        bind(SupplierService.class).to(SupplierServiceImpl.class);
    }
}
