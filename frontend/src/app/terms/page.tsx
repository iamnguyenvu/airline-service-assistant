'use client';

import { ScrollArea } from '@/components/ui/scroll-area';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { PageLayout } from '@/components/layout/PageLayout';

export default function TermsPage() {
  return (
    <PageLayout>
      <div className="flex h-screen flex-col">
      <div className="border-b bg-card p-4 md:p-6">
        <h1 className="text-2xl font-semibold">Điều Khoản & Quy Định</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Điều khoản sử dụng dịch vụ Airline AI Assistant
        </p>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-4 md:p-6 space-y-6 max-w-3xl">
          <Card>
            <CardHeader>
              <CardTitle>1. Giới Thiệu</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>
                Chào mừng bạn đến với Airline AI Assistant. Bằng việc sử dụng dịch vụ của chúng tôi,
                bạn đồng ý tuân thủ các điều khoản và điều kiện được nêu dưới đây.
              </p>
              <p>
                Dịch vụ này được cung cấp để hỗ trợ bạn trong việc tìm kiếm, đặt vé máy bay và
                tư vấn các dịch vụ liên quan đến hàng không.
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>2. Sử Dụng Dịch Vụ</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>
                Bạn cam kết sử dụng dịch vụ một cách hợp pháp và không vi phạm các quyền của
                bên thứ ba. Cụ thể:
              </p>
              <ul className="list-disc list-inside space-y-2 ml-4">
                <li>Không sử dụng dịch vụ cho mục đích bất hợp pháp</li>
                <li>Không cố gắng truy cập trái phép vào hệ thống</li>
                <li>Không tải lên nội dung vi phạm bản quyền</li>
                <li>Cung cấp thông tin chính xác khi đặt vé</li>
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>3. Quyền Riêng Tư</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>
                Chúng tôi cam kết bảo vệ quyền riêng tư của bạn:
              </p>
              <ul className="list-disc list-inside space-y-2 ml-4">
                <li>Thông tin cá nhân được mã hóa và bảo mật</li>
                <li>Không chia sẻ dữ liệu với bên thứ ba không được phép</li>
                <li>Bạn có quyền yêu cầu xóa dữ liệu cá nhân</li>
                <li>Lịch sử chat được lưu trữ an toàn</li>
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>4. Đặt Vé & Thanh Toán</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>
                Khi sử dụng dịch vụ đặt vé:
              </p>
              <ul className="list-disc list-inside space-y-2 ml-4">
                <li>Giá vé có thể thay đổi theo thời gian thực</li>
                <li>Vé đã đặt tuân theo chính sách của hãng hàng không</li>
                <li>Thanh toán được xử lý qua cổng bảo mật</li>
                <li>Hoàn tiền tuân theo điều kiện của vé đã mua</li>
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>5. Trách Nhiệm</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>
                AI Assistant cung cấp thông tin tham khảo. Người dùng cần:
              </p>
              <ul className="list-disc list-inside space-y-2 ml-4">
                <li>Kiểm tra kỹ thông tin trước khi đặt vé</li>
                <li>Xác nhận chi tiết chuyến bay với hãng</li>
                <li>Đọc kỹ điều khoản của từng vé</li>
                <li>Liên hệ hỗ trợ nếu có thắc mắc</li>
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>6. Thay Đổi Điều Khoản</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>
                Chúng tôi có quyền cập nhật điều khoản này. Người dùng sẽ được thông báo về
                các thay đổi quan trọng qua email hoặc thông báo trong ứng dụng.
              </p>
              <p className="mt-4 font-medium text-foreground">
                Cập nhật lần cuối: 11/11/2025
              </p>
            </CardContent>
          </Card>
        </div>
      </ScrollArea>
    </div>
    </PageLayout>
  );
}
