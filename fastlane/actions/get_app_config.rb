require 'uri'
require 'net/http'
require 'json'

module Fastlane
  module Actions
    class GetAppConfigAction < Action
      def self.run(params)
        uri = URI("#{params[:subdomain]}.testpress.in/api/v2.5/admin/android/app-config/")
        http = Net::HTTP.new(uri.host, uri.port)
        http.use_ssl = true
        req = Net::HTTP::Get.new(uri)
        req["API-access-key"] = ENV["API_ACCESS_KEY"]
        res = http.request(req)
        if res.code != '200'
            raise "API request failed, please provide valid subdomain"
        end
        return JSON.parse(res.body)
      end

      def self.available_options
        [
            FastlaneCore::ConfigItem.new(key: :subdomain,description: "Subdomain to get app configuration data")
        ]
       end
    end
  end
end 