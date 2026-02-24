package com.photondns.app.di;

import com.photondns.app.data.database.PhotonDatabase;
import com.photondns.app.data.database.SwitchEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideSwitchEventDaoFactory implements Factory<SwitchEventDao> {
  private final Provider<PhotonDatabase> databaseProvider;

  public DatabaseModule_ProvideSwitchEventDaoFactory(Provider<PhotonDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SwitchEventDao get() {
    return provideSwitchEventDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSwitchEventDaoFactory create(
      Provider<PhotonDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSwitchEventDaoFactory(databaseProvider);
  }

  public static SwitchEventDao provideSwitchEventDao(PhotonDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSwitchEventDao(database));
  }
}
