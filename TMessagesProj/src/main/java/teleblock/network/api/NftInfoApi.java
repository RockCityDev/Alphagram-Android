package teleblock.network.api;


import com.hjq.http.annotation.HttpIgnore;
import com.hjq.http.config.IRequestApi;

/**
 * 更新用户的NFT数据
 */
public class NftInfoApi implements IRequestApi {

    @HttpIgnore
    public String nft_path;

    private String nft_contract;
    private String nft_contract_image;
    private String nft_token_id;
    private String nft_photo_id;
    private String nft_name;
    private long nft_chain_id;
    private String nft_price;
    private String nft_token_standard;

    @Override
    public String getApi() {
        return "/user/nftInfo";
    }

    public NftInfoApi setNft_contract(String nft_contract) {
        this.nft_contract = nft_contract;
        return this;
    }

    public NftInfoApi setNft_contract_image(String nft_contract_image) {
        this.nft_contract_image = nft_contract_image;
        return this;
    }

    public NftInfoApi setNft_token_id(String nft_token_id) {
        this.nft_token_id = nft_token_id;
        return this;
    }

    public NftInfoApi setNft_photo_id(String nft_photo_id) {
        this.nft_photo_id = nft_photo_id;
        return this;
    }

    public NftInfoApi setNft_path(String nft_path) {
        this.nft_path = nft_path;
        return this;
    }

    public NftInfoApi setNft_name(String nft_name) {
        this.nft_name = nft_name;
        return this;
    }

    public NftInfoApi setNft_chain_id(long nft_chain_id) {
        this.nft_chain_id = nft_chain_id;
        return this;
    }

    public NftInfoApi setNft_price(String nft_price) {
        this.nft_price = nft_price;
        return this;
    }

    public NftInfoApi setNft_token_standard(String nft_token_standard) {
        this.nft_token_standard = nft_token_standard;
        return this;
    }
}
