require 'tempfile'
require 'fileutils'
require 'json'

module Fastlane
  module Actions
    class ModifyConfigJsonFileAction < Action
      def self.run(params)     
        fields_to_change = ["allow_anonymous_user","package_name", "app_name", "primary_color", "server_client_id","facebook_app_id", 
                            "display_username_on_video", "white_labeled_host_url", "is_growth_hacks_enabled", "show_pdf_vertically", "testpress_site_subdomain", "share_message", "secondary_color","tertiary_color", "zoom_enabled"]
        replace_fields_value(fields_to_change, params[:config_json])
      end

      def self.replace_fields_value(fields, data)
          path = "./app/src/main/assets/config.json"
          file = File.read(path)
          current_json = JSON.parse(file)
          json_data = JSON.parse(data)
          current_json.each{|key, value| current_json[key] = json_data[key] if json_data.key?(key)}
          File.open(path,"w") do |f|
            f.puts JSON.pretty_generate(current_json)
          end       
      end
      def self.available_options
        [
           FastlaneCore::ConfigItem.new(key: :config_json,
                                        description: "Json file with value to update")

        ]
      end
    end
  end
end