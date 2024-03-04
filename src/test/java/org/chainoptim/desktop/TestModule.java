package org.chainoptim.desktop;

import com.google.inject.AbstractModule;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import static org.mockito.Mockito.mock;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FXMLLoaderService.class).toInstance(mock(FXMLLoaderService.class));
    }
}
