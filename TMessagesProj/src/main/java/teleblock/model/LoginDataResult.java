package teleblock.model;

/**
 * Time:2022/7/28
 * Author:Perry
 * Description：登录数据返回
 */
public class LoginDataResult {
    private boolean newer;
    private UserEntity user;
    private String token;

    public boolean isNewer() {
        return newer;
    }

    public void setNewer(boolean newer) {
        this.newer = newer;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public final static class UserEntity {
        private String name;
        private String avatar;
        private String desc;
        private String device;
        private int platform;
        private String user_id;
        private int is_show_wallet;

        public void setIs_show_wallet(int is_show_wallet) {
            this.is_show_wallet = is_show_wallet;
        }

        public int getIs_show_wallet() {
            return is_show_wallet;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public int getPlatform() {
            return platform;
        }

        public void setPlatform(int platform) {
            this.platform = platform;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }
    }
}
