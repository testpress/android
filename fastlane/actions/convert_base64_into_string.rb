require "base64"
require 'json'

module Fastlane
  module Actions
    class ConvertBase64IntoStringAction < Action
      def self.run(params)
        return Base64.decode64(params[:data])
      end
      def self.available_options
        [
           FastlaneCore::ConfigItem.new(key: :data, env_name: "ConvertBase64IntoString",
                                        description: "major",
                                        default_value: "",
                                        verify_block: proc do |value|
                                        end)

        ]
       end
    end
  end
end