import { VuexModule, Module, Action, Mutation, getModule } from 'vuex-module-decorators';
import { login } from '@/api/users';
import { getToken, setToken, removeToken,getStoreId, setStoreId, removeStoreId, setUserInfo, getUserInfo, removeUserInfo } from '@/utils/cookies';
import store from '@/store';

export interface IUserState {
  token: string
  name: string
  avatar: string
  storeId: string
  introduction: string
  userInfo: any
  roles: string[]
}

@Module({ 'dynamic': true, store, 'name': 'user' })
class User extends VuexModule implements IUserState {
  public token = getToken() || ''
  public name = ''
  public avatar = ''
  public storeId = getStoreId() || ''
  public introduction = ''
  public userInfo = {}
  public roles: string[] = []

  @Mutation
  private SET_TOKEN(token: string) {
      this.token = token;
  }

  @Mutation
  private SET_NAME(name: string) {
      this.name = name;
  }

  @Mutation
  private SET_USERINFO(userInfo: string) {
      this.userInfo = userInfo;
  }

  @Mutation
  private SET_AVATAR(avatar: string) {
      this.avatar = avatar;
  }

  @Mutation
  private SET_INTRODUCTION(introduction: string) {
      this.introduction = introduction;
  }

  @Mutation
  private SET_ROLES(roles: string[]) {
      this.roles = roles;
  }

  @Mutation
  private SET_STOREID(storeId: string[]) {
      this.storeId = storeId;
  }

  @Action
  public async Login(userInfo: { shopId: string, phone: string, password: string, type: number }) {
      let { shopId, phone, password, type } = userInfo;
      phone = phone.trim();
      const { data } = await login({ shopId, phone, password, type });
      setToken(data.authorization);
      this.SET_TOKEN(data.authorization);
      this.SET_NAME(data.data.applicant || data.data.storeManagerName);
      if (type === 1) {
          this.SET_ROLES(['admin']);
      } else if (type === 2) {
          this.SET_ROLES(['shop']);
      } else {
          this.SET_ROLES(['member']);
      }
      this.SET_STOREID('');
      data.data.roles = this.roles;
      setUserInfo(data.data);
  }

  @Action
  public ResetToken() {
      removeToken();
      this.SET_TOKEN('');
      this.SET_ROLES([]);
  }

  @Action
  public async changeStore(data: any) {
      this.SET_STOREID = data.data;
      this.SET_TOKEN(data.authorization);
      setStoreId(data.data);
  }

  @Action
  public async GetUserInfo() {
      if (this.token === '') {
          throw Error('GetUserInfo: token is undefined!');
      }

      const data = JSON.parse(<string>getUserInfo()); //  { roles: ['admin'], name: 'zhangsan', avatar: '/login', introduction: '' }
      if (!data) {
          throw Error('Verification failed, please Login again.');
      }

      const { roles, name, avatar, introduction, applicant, storeManagerName, storeId='' } = data; // data.user
      // roles must be a non-empty array
      if (!roles || roles.length <= 0) {
          throw Error('GetUserInfo: roles must be a non-null array!');
      }

      this.SET_ROLES(roles);
      this.SET_USERINFO(data);
      this.SET_NAME(name || applicant || storeManagerName);
      this.SET_AVATAR(avatar);
      this.SET_INTRODUCTION(introduction);
  }

  @Action
  public async LogOut() {
      if (this.token === '') {
          throw Error('LogOut: token is undefined!');
      }
      removeToken();
      removeStoreId();
      removeUserInfo();
      this.SET_TOKEN('');
      this.SET_ROLES([]);
  }
}

export const UserModule = getModule(User);
