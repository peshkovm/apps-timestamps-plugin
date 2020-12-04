declare module '@capacitor/core' {
  interface PluginRegistry {
    AppsTimestampsPlugin: AppsTimestampsPluginPlugin;
  }
}

export interface AppsTimestampsPluginPlugin {
  getAppsTimestamps(): Promise<{ value: any[] }>;
}
