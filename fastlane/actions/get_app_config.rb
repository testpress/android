require 'uri'
require 'net/http'

module Fastlane
  module Actions
    class GetAppConfigAction < Action
      def self.run(params)
        puts "Inside get_app config actions"
        uri = URI('https://hari.in.ngrok.io/api/android/')
        res = Net::HTTP.get_response(uri)
        puts "#{res.code} , #{res.message}"
        puts "-------------------------------------------"
        uri = URI('https://reqres.in/api/users/')
        res1 = Net::HTTP.get_response(uri)
        puts "#{res1.code} , #{res1.message}"
        return res.body if res.is_a?(Net::HTTPSuccess)
      end
    end
  end
end