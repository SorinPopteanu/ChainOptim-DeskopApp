package org.chainoptim.desktop;

import com.google.inject.AbstractModule;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.repository.UserRepositoryImpl;

/*
 * Module configuring Dependency Injections
 *
 */
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserRepository.class).to(UserRepositoryImpl.class);
    }
}
