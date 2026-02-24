package com.photondns.app.data.repository;

import com.photondns.app.data.database.LatencyDao;
import com.photondns.app.data.database.SwitchEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class LatencyRepository_Factory implements Factory<LatencyRepository> {
  private final Provider<LatencyDao> latencyDaoProvider;

  private final Provider<SwitchEventDao> switchEventDaoProvider;

  public LatencyRepository_Factory(Provider<LatencyDao> latencyDaoProvider,
      Provider<SwitchEventDao> switchEventDaoProvider) {
    this.latencyDaoProvider = latencyDaoProvider;
    this.switchEventDaoProvider = switchEventDaoProvider;
  }

  @Override
  public LatencyRepository get() {
    return newInstance(latencyDaoProvider.get(), switchEventDaoProvider.get());
  }

  public static LatencyRepository_Factory create(Provider<LatencyDao> latencyDaoProvider,
      Provider<SwitchEventDao> switchEventDaoProvider) {
    return new LatencyRepository_Factory(latencyDaoProvider, switchEventDaoProvider);
  }

  public static LatencyRepository newInstance(LatencyDao latencyDao,
      SwitchEventDao switchEventDao) {
    return new LatencyRepository(latencyDao, switchEventDao);
  }
}
