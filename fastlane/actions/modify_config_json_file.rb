require 'tempfile'
require 'fileutils'
require 'json'

module Fastlane
  module Actions
    class ModifyConfigJsonFileAction < Action
      def self.run(params)     
        fields_to_change = [
          "version",
          "version_code",
          "allow_anonymous_user",
          "package_name", 
          "app_name",
          "share_message",
          "primary_color", 
          "server_client_id",
          "facebook_app_id",          
          "display_username_on_video", 
          "white_labeled_host_url", 
          "is_growth_hacks_enabled", 
          "show_pdf_vertically", 
          "testpress_site_subdomain", 
          "secondary_color",
          "tertiary_color", 
          "zoom_enabled"
        ]
        replace_fields_value(fields_to_change, params[:config_json])
      end

      def self.replace_fields_value(fields, data)
          path = "./app/src/main/assets/config.json"
          file = File.read(path)
          config_json = JSON.parse(file)
          app_config_json = JSON.parse(data)
          config_json.each do |key, value|
            if app_config_json.key?(key) && fields.include?(key)
                puts "#{key}: #{config_json[key]}" # Print app_name
                if key == "app_name" or "share_message"
                    temp_value = app_config_json[key]
                    # Replace single quotes with escaped single quotes
                    config_json[key] = temp_value.gsub("\'", "\\\\'")
                else
                    config_json[key] = app_config_json[key]
                end
            end
          end
          File.open(path,"w") do |f|
            f.puts JSON.pretty_generate(config_json)
          end  
      end
      def self.available_options
        [
           FastlaneCore::ConfigItem.new(key: :config_json, description: "Json file with value to update")
        ]
      end
    end
  end
end