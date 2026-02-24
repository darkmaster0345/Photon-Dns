package com.photondns.app.presentation.viewmodel;

import com.photondns.app.service.DNSSwitchManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<DNSSwitchManager> dnsSwitchManagerProvider;

  public SettingsViewModel_Factory(Provider<DNSSwitchManager> dnsSwitchManagerProvider) {
    this.dnsSwitchManagerProvider = dnsSwitchManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(dnsSwitchManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<DNSSwitchManager> dnsSwitchManagerProvider) {
    return new SettingsViewModel_Factory(dnsSwitchManagerProvider);
  }

  public static SettingsViewModel newInstance(DNSSwitchManager dnsSwitchManager) {
    return new SettingsViewModel(dnsSwitchManager);
  }
}
