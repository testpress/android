require 'uri'
require 'net/http'

module Fastlane
  module Actions
    class GetAppConfigAction < Action
      def self.run(params)
        uri = URI('https://hari.in.ngrok.io/api/android/')
        res = Net::HTTP.get_response(uri)
        return res.body if res.is_a?(Net::HTTPSuccess)
      end
    end
  end
end