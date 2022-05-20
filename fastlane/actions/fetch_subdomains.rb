require 'uri'
require 'net/http'
require 'json'

module Fastlane
  module Actions
    class FetchSubdomainsAction < Action
      def self.run(params)
        uri = URI("https://sandbox.testpress.in/api/v2.5/admin/android/subdomains/")
        request = Net::HTTP::Get.new(uri)
        request["API-access-key"] = ENV["API_ACCESS_KEY"]
        http = Net::HTTP.new(uri.host, uri.port)
        http.use_ssl = true
        response = http.request(request)
        return JSON.parse(response.body)
      end
    end
  end
end