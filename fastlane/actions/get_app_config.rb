require 'uri'
require 'net/http'
require 'json'

module Fastlane
  module Actions
    class GetAppConfigAction < Action
      def self.run(params)
        uri = URI("#{params[:subdomain]}/api/v2.5/admin/android/app-config/")
        request = Net::HTTP::Get.new(uri)
        request["API-access-key"] = ENV["API_ACCESS_KEY"]
        http = Net::HTTP.new(uri.host, uri.port)
        http.use_ssl = true
        response = http.request(request)
        return JSON.parse(response.body)
      end

      def self.available_options
        [
            FastlaneCore::ConfigItem.new(key: :subdomain,description: "Subdomain to get app configuration data")
        ]
       end
    end
  end
end 