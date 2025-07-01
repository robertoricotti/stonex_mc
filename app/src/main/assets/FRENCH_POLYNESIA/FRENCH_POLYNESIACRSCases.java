public class FRENCH_POLYNESIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FRENCH_POLYNESIA__144_W_TO_138_W__RGPF__UTM_ZONE_7S__3298.SP":
                MyData.push("crs", "3298");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__150_W_TO_144_W__RGPF__UTM_ZONE_6S__3297.SP":
                MyData.push("crs", "3297");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__EAST_OF_138_W__RGPF__UTM_ZONE_8S__3299.SP":
                MyData.push("crs", "3299");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__MARQUESAS_ISLANDS__FATU_HIVA__FATU_IVA_72__UTM_ZONE_7S__3303.SP":
                MyData.push("crs", "3303");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__MARQUESAS_ISLANDS__HIVA_OA_AND_TAHUATA__IGN63_HIVA_OA__UTM_ZONE_7S__3302.SP":
                MyData.push("crs", "3302");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__MARQUESAS_ISLANDS__NUKU_HIVA__UA_HUKA_AND_UA_POU__IGN72_NUKU_HIVA__UTM_ZONE_7S__2978.SP":
                MyData.push("crs", "2978");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__RGPF__4998.SP":
                MyData.push("crs", "4998");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__SOCIETY_ISLANDS__BORA_BORA__HUAHINE__RAIATEA__TAHAA__TAHAA_54__UTM_ZONE_5S__2977.SP":
                MyData.push("crs", "2977");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__SOCIETY_ISLANDS__MAUPITI__MAUPITI_83__UTM_ZONE_5S__3306.SP":
                MyData.push("crs", "3306");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__SOCIETY_ISLANDS__MOOREA_AND_TAHITI__TAHITI_52__UTM_ZONE_6S__2976.SP":
                MyData.push("crs", "2976");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__SOCIETY_ISLANDS__MOOREA__MOOREA_87__UTM_ZONE_6S__3305.SP":
                MyData.push("crs", "3305");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__SOCIETY_ISLANDS__TAHITI__TAHITI_79__UTM_ZONE_6S__3304.SP":
                MyData.push("crs", "3304");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_POLYNESIA__WEST_OF_150_W__RGPF__UTM_ZONE_5S__3296.SP":
                MyData.push("crs", "3296");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}