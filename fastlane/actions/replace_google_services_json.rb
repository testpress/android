require 'tempfile'
require 'fileutils'
require 'json'

module Fastlane
  module Actions
    class ReplaceGoogleServicesJsonAction < Action
      def self.run(params)
        google_services_hash = JSON.parse(params[:google_services_json])
        File.open('./app/google-services.json', 'w') do |f|
          f.puts JSON.pretty_generate(google_services_hash)
        end
      end
      def self.available_options
        [
           FastlaneCore::ConfigItem.new(key: :google_services_json)
        ]
      end
    end
   end
end 