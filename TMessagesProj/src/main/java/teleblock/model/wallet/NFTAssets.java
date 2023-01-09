package teleblock.model.wallet;

import com.blankj.utilcode.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.util.JsonUtil;


public class NFTAssets {

    public String next;
    public Object previous;
    public List<AssetsEntity> assets; // v1
    public List<AssetsEntity> results; // v2

    public static NFTInfoList parse(String result) {
        NFTAssets nftAssets = JsonUtil.parseJsonToBean(result, NFTAssets.class);
        NFTInfoList nftInfoList = new NFTInfoList();
        List<NFTInfo> nftInfos = new ArrayList<>();
        if (nftAssets == null) {
            return nftInfoList;
        }
        List<AssetsEntity> list;
        if (nftAssets.assets == null) {
            list = nftAssets.results;
        } else {
            list = nftAssets.assets;
        }
        if (list == null) {
            return nftInfoList;
        }
        for (AssetsEntity assetsEntity : list) {
            if (assetsEntity.collection.hidden) {
                continue; // 先这样过滤
            }
            nftInfos.add(AssetsEntity.parse(assetsEntity, 0));
        }
        nftInfoList.assets = nftInfos;
        nftInfoList.next = nftAssets.next;
        return nftInfoList;
    }

    public static class AssetsEntity {
        public int id;
        public int num_sales;
        public Object background_color;
        public String image_url;
        public String image_preview_url;
        public String image_thumbnail_url;
        public String image_original_url;
        public String animation_url;
        public String animation_original_url;
        public String name;
        public String description;
        public String external_link;
        public AssetContractEntity asset_contract;
        public String permalink;
        public CollectionEntity collection;
        public Object decimals;
        public String token_metadata;
        public boolean is_nsfw;
        public OwnerEntity owner;
        public List<SeaportSellOrdersEntity> seaport_sell_orders;
        public CreatorEntity creator;
        public List<TraitsEntity> traits;
        public LastSaleEntity last_sale;
        public Object top_bid;
        public Object listing_date;
        public boolean is_presale;
        public boolean supports_wyvern;
        public Object rarity_data;
        public Object transfer_fee;
        public Object transfer_fee_payment_token;
        public String token_id;

        public static NFTInfo parse(AssetsEntity assetsEntity, long chain_id) {
            NFTInfo nftInfo = new NFTInfo();
            try {
                nftInfo.asset_name = assetsEntity.collection.name;
                nftInfo.nft_name = assetsEntity.name;
                if (assetsEntity.name == null) {
                    nftInfo.nft_name = assetsEntity.token_id;
                }
                nftInfo.thumb_url = assetsEntity.image_thumbnail_url;
                nftInfo.original_url = assetsEntity.image_url;
                nftInfo.token_id = assetsEntity.token_id;
                nftInfo.contract_address = assetsEntity.asset_contract.address;
                nftInfo.symbol = assetsEntity.asset_contract.symbol;
                if (!CollectionUtils.isEmpty(assetsEntity.seaport_sell_orders)) {
                    nftInfo.price = assetsEntity.seaport_sell_orders.get(0).current_price;
                } else if (assetsEntity.last_sale != null) {
                    nftInfo.price = assetsEntity.last_sale.total_price;
                }
                nftInfo.token_standard = assetsEntity.asset_contract.schema_name;
                WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = BlockchainConfig.getChainType(chain_id);
                if (chainType != null) {
                    nftInfo.blockchain = chainType.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return nftInfo;
        }

        public static class AssetContractEntity {
            public String address;
            public String asset_contract_type;
            public String created_date;
            public String name;
            public String nft_version;
            public Object opensea_version;
            public String owner;
            public String schema_name;
            public String symbol;
            public String total_supply;
            public String description;
            public String external_link;
            public String image_url;
            public boolean default_to_fiat;
            public int dev_buyer_fee_basis_points;
            public int dev_seller_fee_basis_points;
            public boolean only_proxied_transfers;
            public int opensea_buyer_fee_basis_points;
            public int opensea_seller_fee_basis_points;
            public int buyer_fee_basis_points;
            public int seller_fee_basis_points;
            public String payout_address;
        }

        public static class CollectionEntity {
            public String banner_image_url;
            public Object chat_url;
            public String created_date;
            public boolean default_to_fiat;
            public String description;
            public String dev_buyer_fee_basis_points;
            public String dev_seller_fee_basis_points;
            public Object discord_url;
            public DisplayDataEntity display_data;
            public String external_url;
            public boolean featured;
            public String featured_image_url;
            public boolean hidden;
            public String safelist_request_status;
            public String image_url;
            public boolean is_subject_to_whitelist;
            public String large_image_url;
            public Object medium_username;
            public String name;
            public boolean only_proxied_transfers;
            public String opensea_buyer_fee_basis_points;
            public String opensea_seller_fee_basis_points;
            public String payout_address;
            public boolean require_email;
            public Object short_description;
            public String slug;
            public Object telegram_url;
            public String twitter_username;
            public Object instagram_username;
            public Object wiki_url;
            public boolean is_nsfw;

            public static class DisplayDataEntity {
                public String card_display_style;
            }
        }

        public static class OwnerEntity {
            public UserEntity user;
            public String profile_img_url;
            public String address;
            public String config;

            public static class UserEntity {
                public String username;
            }
        }

        public static class CreatorEntity {
            public UserEntity user;
            public String profile_img_url;
            public String address;
            public String config;

            public static class UserEntity {
                public String username;
            }
        }

        public static class LastSaleEntity {
            public AssetEntity asset;
            public Object asset_bundle;
            public String event_type;
            public String event_timestamp;
            public Object auction_type;
            public String total_price;
            public PaymentTokenEntity payment_token;
            public Object transaction;
            public String created_date;
            public String quantity;

            public static class AssetEntity {
                public Object decimals;
                public String token_id;
            }

            public static class PaymentTokenEntity {
                public String symbol;
                public String address;
                public String image_url;
                public String name;
                public int decimals;
                public String eth_price;
                public String usd_price;
            }
        }

        public static class SeaportSellOrdersEntity {
            public String created_date;
            public String closing_date;
            public int listing_time;
            public int expiration_time;
            public String order_hash;
            public ProtocolDataEntity protocol_data;
            public String protocol_address;
            public MakerEntity maker;
            public Object taker;
            public String current_price;
            public List<MakerFeesEntity> maker_fees;
            public List<?> taker_fees;
            public String side;
            public String order_type;
            public boolean cancelled;
            public boolean finalized;
            public boolean marked_invalid;
            public String client_signature;
            public String relay_id;
            public Object criteria_proof;

            public static class ProtocolDataEntity {
                public ParametersEntity parameters;
                public String signature;

                public static class ParametersEntity {
                    public String offerer;
                    public List<OfferEntity> offer;
                    public List<ConsiderationEntity> consideration;
                    public String startTime;
                    public String endTime;
                    public int orderType;
                    public String zone;
                    public String zoneHash;
                    public String salt;
                    public String conduitKey;
                    public int totalOriginalConsiderationItems;
                    public int counter;

                    public static class OfferEntity {
                        public int itemType;
                        public String token;
                        public String identifierOrCriteria;
                        public String startAmount;
                        public String endAmount;
                    }

                    public static class ConsiderationEntity {
                        public int itemType;
                        public String token;
                        public String identifierOrCriteria;
                        public String startAmount;
                        public String endAmount;
                        public String recipient;
                    }
                }
            }

            public static class MakerEntity {
                public int user;
                public String profile_img_url;
                public String address;
                public String config;
            }

            public static class MakerFeesEntity {
                public AccountEntity account;
                public String basis_points;

                public static class AccountEntity {
                    public int user;
                    public String profile_img_url;
                    public String address;
                    public String config;
                }
            }
        }

        public static class TraitsEntity {
            public String trait_type;
            public String value;
            public Object display_type;
            public Object max_value;
            public int trait_count;
            public Object order;
        }
    }
}
