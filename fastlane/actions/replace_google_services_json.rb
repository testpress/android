require 'tempfile'
require 'fileutils'

module Fastlane
  module Actions
    class ReplaceGoogleServicesJsonAction < Action
      def self.run(params)
        File.open('./app/google-services.json', 'wb') do |f|
          f.write(params[:google_services_json])
        end
      end
      def self.available_options
        [
           FastlaneCore::ConfigItem.new(key: :google_services_json, 
                                        env_name: "ReplaceBase64StringFile",
                                        description: "major",
                                        default_value: "",
                                        verify_block: proc do |value|
                                        end)

        ]
      end
    end
   end
end