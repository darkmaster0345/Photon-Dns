package com.photondns.app.di;

import android.content.Context;
import com.photondns.app.data.database.PhotonDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvidePhotonDatabaseFactory implements Factory<PhotonDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvidePhotonDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PhotonDatabase get() {
    return providePhotonDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvidePhotonDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvidePhotonDatabaseFactory(contextProvider);
  }

  public static PhotonDatabase providePhotonDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePhotonDatabase(context));
  }
}
