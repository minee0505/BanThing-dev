import {createBrowserRouter} from "react-router-dom";
import AppLayout from "../layouts/AppLayout.jsx";
import ErrorPage from "../pages/ErrorPage.jsx";
import MeetingListPage from "../pages/MeetingListPage.jsx";
import MeetingDetailPage from "../pages/MeetingDetailPage.jsx";
// import CommentPage from "../pages/CommentPage.jsx";
import LoginPage from "../pages/LoginPage.jsx";
import AgreementPage from "../pages/AgreementPage.jsx";
import PopupClosePage from '../pages/PopupClosePage.jsx';
import LoadMeRoute from "../components/Auth/LoadMeRoute.jsx";
import MeetingCreatePage from "../pages/MeetingCreatePage.jsx";
import PrivateRoute from "../components/Auth/PrivateRoute.jsx";
import ProfilePage from "../pages/ProfilePage.jsx";
import {profileMeetingLoader} from "../loaders/profileMeetingLoader.js";

// 라우터 설정
export const router = createBrowserRouter([
  {
    path: '/',
    element: (
        <LoadMeRoute>
          <AppLayout />
        </LoadMeRoute>
    ),
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <MeetingListPage /> },
      // [수정] MeetingCreatePage를 PrivateRoute로 감싸줍니다.
      {
        path: 'meetings/new',
        element: (
            <PrivateRoute>
              <MeetingCreatePage />
            </PrivateRoute>
        )
      },
      { path: 'meetings/:id', element: <MeetingDetailPage /> },
      // { path: 'meetings/:id/comments', element: <CommentPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'profile/:id', element: <ProfilePage />, loader: profileMeetingLoader},
    ],
  },
  {
    path: '/agreement',
    element: <AgreementPage />
  },
  {
    path: '/popup-close', // 팝업을 닫아주는 페이지
    element: <PopupClosePage />
  }
]);

export default router;