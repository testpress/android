require 'uri'
require 'net/http'
require 'json'

module Fastlane
  module Actions
    class UpdateAppVersionAction < Action
      def self.run(params)
        uri = URI("https://#{params[:subdomain]}.testpress.in/api/v2.5/admin/android/update/")
        request = Net::HTTP::Put.new(uri)
        request["API-access-key"] = ENV["API_ACCESS_KEY"]
        request.body = " "
        http = Net::HTTP.new(uri.host, uri.port)
        http.use_ssl = true
        response = http.request(request)
        return JSON.parse(response.body)
      end

      def self.available_options
        [
            FastlaneCore::ConfigItem.new(key: :subdomain,description: "Subdomain to get update configuration")
        ]
       end
    end
  end
end 