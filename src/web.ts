import { WebPlugin } from '@capacitor/core';
import { AppsTimestampsPluginPlugin } from './definitions';

export class AppsTimestampsPluginWeb extends WebPlugin implements AppsTimestampsPluginPlugin {
  constructor() {
    super({
      name: 'AppsTimestampsPlugin',
      platforms: ['web'],
    });
  }

  async getAppsTimestamps(): Promise<{ value: any[] }> {
    throw new Error('getNumOfActiveApps is not implemented in web. ' +
        'You should run your app on Android');
  }
}

const AppsTimestampsPlugin = new AppsTimestampsPluginWeb();

export { AppsTimestampsPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AppsTimestampsPlugin);
